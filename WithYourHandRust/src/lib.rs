use axum::{
    Json, Router,
    extract::{DefaultBodyLimit, rejection::JsonRejection},
    http::StatusCode,
    routing::{get, post},
};
use serde::{Deserialize, Serialize};

const MAX_NUMBERS: usize = 100_000;

pub fn app() -> Router {
    Router::new()
        .route("/health", get(health))
        .route("/api/rust/info", get(info))
        .route("/api/rust/stats", post(statistics))
        .layer(DefaultBodyLimit::max(1024 * 1024))
}

#[derive(Serialize)]
struct Health {
    status: &'static str,
}

async fn health() -> Json<Health> {
    Json(Health { status: "UP" })
}

#[derive(Serialize)]
struct ServiceInfo {
    service: &'static str,
    language: &'static str,
    version: &'static str,
}

async fn info() -> Json<ServiceInfo> {
    Json(ServiceInfo {
        service: "WithYourHandRust",
        language: "Rust",
        version: env!("CARGO_PKG_VERSION"),
    })
}

#[derive(Deserialize)]
struct StatisticsRequest {
    numbers: Vec<i32>,
}

#[derive(Debug, Serialize, PartialEq)]
struct Statistics {
    count: usize,
    sum: i64,
    min: i32,
    max: i32,
    mean: f64,
}

#[derive(Serialize)]
struct ApiError {
    code: &'static str,
    message: &'static str,
}

type ApiResult = Result<Json<Statistics>, (StatusCode, Json<ApiError>)>;

fn calculate(numbers: &[i32]) -> Option<Statistics> {
    let (&first, remaining) = numbers.split_first()?;
    let (sum, min, max) = remaining.iter().fold(
        (i64::from(first), first, first),
        |(sum, min, max), &number| (sum + i64::from(number), min.min(number), max.max(number)),
    );
    Some(Statistics {
        count: numbers.len(),
        sum,
        min,
        max,
        mean: sum as f64 / numbers.len() as f64,
    })
}

async fn statistics(payload: Result<Json<StatisticsRequest>, JsonRejection>) -> ApiResult {
    let Json(request) = payload.map_err(|rejection| {
        (
            rejection.status(),
            Json(ApiError {
                code: "INVALID_JSON",
                message: "Expected JSON with a numbers array of 32-bit integers; body limit is 1 MiB.",
            }),
        )
    })?;
    if request.numbers.is_empty() || request.numbers.len() > MAX_NUMBERS {
        return Err((
            StatusCode::BAD_REQUEST,
            Json(ApiError {
                code: "INVALID_ARGUMENT",
                message: "numbers must contain between 1 and 100000 integers.",
            }),
        ));
    }
    Ok(Json(calculate(&request.numbers).expect("nonempty input")))
}

#[cfg(test)]
mod tests {
    use super::*;
    use axum::{body::Body, http::Request};
    use tower::ServiceExt;

    async fn post_stats(body: &str) -> axum::response::Response {
        app()
            .oneshot(
                Request::post("/api/rust/stats")
                    .header("content-type", "application/json")
                    .body(Body::from(body.to_owned()))
                    .unwrap(),
            )
            .await
            .unwrap()
    }

    #[test]
    fn handles_negatives_and_uses_wide_sum() {
        assert_eq!(calculate(&[]), None);
        assert_eq!(
            calculate(&[-2, 2, 6]),
            Some(Statistics {
                count: 3,
                sum: 6,
                min: -2,
                max: 6,
                mean: 2.0,
            })
        );
        assert_eq!(calculate(&[i32::MAX, i32::MAX]).unwrap().sum, 4_294_967_294);
    }

    #[tokio::test]
    async fn returns_statistics_over_http() {
        let response = post_stats(r#"{"numbers":[1,2,3,4,5]}"#).await;
        assert_eq!(response.status(), StatusCode::OK);
        let body = axum::body::to_bytes(response.into_body(), 1024)
            .await
            .unwrap();
        let value: serde_json::Value = serde_json::from_slice(&body).unwrap();
        assert_eq!(value["sum"], 15);
        assert_eq!(value["mean"], 3.0);
    }

    #[tokio::test]
    async fn rejects_invalid_input_and_large_requests() {
        assert_eq!(
            post_stats(r#"{"numbers":[]}"#).await.status(),
            StatusCode::BAD_REQUEST
        );
        assert_eq!(
            post_stats(r#"{"numbers":[1.5]}"#).await.status(),
            StatusCode::UNPROCESSABLE_ENTITY
        );
        assert_eq!(post_stats("{").await.status(), StatusCode::BAD_REQUEST);
        let over_limit = serde_json::json!({"numbers": vec![0; MAX_NUMBERS + 1]}).to_string();
        assert_eq!(
            post_stats(&over_limit).await.status(),
            StatusCode::BAD_REQUEST
        );
        assert_eq!(
            post_stats(&" ".repeat(1024 * 1024 + 1)).await.status(),
            StatusCode::PAYLOAD_TOO_LARGE
        );
    }

    #[tokio::test]
    async fn health_is_available() {
        let response = app()
            .oneshot(Request::get("/health").body(Body::empty()).unwrap())
            .await
            .unwrap();
        assert_eq!(response.status(), StatusCode::OK);
    }
}
