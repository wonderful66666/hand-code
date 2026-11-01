export async function request(path, options = {}) {
  const response = await fetch(path, { ...options, signal: AbortSignal.timeout(5000) })
  const text = await response.text()
  let data
  try {
    data = JSON.parse(text)
  } catch {
    throw new Error('服务未启动或未返回 JSON，请检查后端终端。')
  }
  if (!response.ok) throw new Error(data.message || `请求失败：HTTP ${response.status}`)
  return data
}

export const services = [
  { id: 'qbh', name: 'WithYourHandQBH', owner: 'QBH 的练习空间', stack: 'JAVA / SPRING BOOT', port: 8081 },
  { id: 'ymy', name: 'WithYourHandYmy', owner: 'Ymy 的练习空间', stack: 'JAVA / SPRING BOOT', port: 8082 },
  { id: 'rust', name: 'WithYourHandRust', owner: '计算与性能实验室', stack: 'RUST / AXUM', port: 8083 },
]

export async function checkServices() {
  return Object.fromEntries(await Promise.all(services.map(async ({ id }) => {
    try {
      const info = await request(`/api/${id}/info`)
      return [id, { online: true, info }]
    } catch {
      return [id, { online: false }]
    }
  })))
}
