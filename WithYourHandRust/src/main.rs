use std::{env, net::SocketAddr};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let address: SocketAddr = env::var("RUST_ADDRESS")
        .unwrap_or_else(|_| "127.0.0.1:8083".to_owned())
        .parse()?;
    let listener = tokio::net::TcpListener::bind(address).await?;
    println!(
        "WithYourHandRust listening on http://{}",
        listener.local_addr()?
    );
    axum::serve(listener, with_your_hand_rust::app())
        .with_graceful_shutdown(async {
            if let Err(error) = tokio::signal::ctrl_c().await {
                eprintln!("Could not listen for Ctrl+C: {error}");
            }
        })
        .await?;
    Ok(())
}
