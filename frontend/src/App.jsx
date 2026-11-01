import { useEffect, useState } from 'react'
import { checkServices, request, services } from './api.js'

export default function App() {
  const [statuses, setStatuses] = useState({})
  const [checking, setChecking] = useState(true)
  const [service, setService] = useState('qbh')
  const [name, setName] = useState('朋友')
  const [numbers, setNumbers] = useState('1, 2, 3, 4, 5')
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [pending, setPending] = useState(false)

  useEffect(() => {
    let active = true
    checkServices().then((value) => {
      if (active) {
        setStatuses(value)
        setChecking(false)
      }
    })
    return () => { active = false }
  }, [])

  async function refresh() {
    setChecking(true)
    setStatuses(await checkServices())
    setChecking(false)
  }

  async function run(event) {
    event.preventDefault()
    setPending(true)
    setError('')
    setResult(null)
    try {
      if (service === 'rust') {
        const values = numbers.split(/[,，\s]+/).filter(Boolean).map(Number)
        if (!values.length || values.length > 100000 || values.some((value) => !Number.isInteger(value) || value < -2147483648 || value > 2147483647)) {
          throw new Error('请输入 1–100000 个 32 位整数，用逗号或空格分隔。')
        }
        setResult(await request('/api/rust/stats', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ numbers: values }),
        }))
      } else {
        setResult(await request(`/api/${service}/hello?name=${encodeURIComponent(name)}`))
      }
    } catch (failure) {
      setError(failure.name === 'TimeoutError' ? '请求超时，请检查对应服务是否启动。' : failure.message)
    } finally {
      setPending(false)
    }
  }

  return (
    <main>
      <header className="topbar">
        <a className="brand" href="/" aria-label="WithYourHand 首页"><span className="brand-mark">W</span> WithYourHand</a>
        <a className="repo" href="https://github.com/wonderful66666/hand-code" target="_blank" rel="noreferrer">GitHub ↗</a>
      </header>

      <section className="hero">
        <div className="eyebrow"><span /> THE 100-DAY CODING PROJECT</div>
        <h1>100 天，<br />动手<span className="accent">就好。</span></h1>
        <p className="intro">两个伙伴，三种技术，无限个想法。<br />从一个小 API 开始，把好奇心写成代码。</p>
        <div className="stack"><span>React</span><i>+</i><span>Java 25</span><i>+</i><span>Rust 1.93</span></div>
        <div className="hero-note"><span>001 — 100</span><p>每天一点，持续生长。<br />今天想写什么，就从这里开始。</p></div>
      </section>

      <section className="workspace" aria-labelledby="workspace-title">
        <div className="section-heading"><div><p className="eyebrow">YOUR WORKSPACE</p><h2 id="workspace-title">三个服务，各自生长</h2></div><button className="text-button" onClick={refresh} disabled={checking}>{checking ? '检测中…' : '刷新状态 ↻'}</button></div>
        <div className="services">
          {services.map((item, index) => (
            <article className="service-card" key={item.id}>
              <div className="card-top"><span className="index">0{index + 1}</span><span className={`status ${statuses[item.id]?.online ? 'online' : ''}`}><b />{checking ? '检测中' : statuses[item.id]?.online ? '已连接' : '未连接'}</span></div>
              <p className="technology">{item.stack}</p><h3>{item.name}</h3><p className="muted">{item.owner}</p>
              <div className="card-bottom"><code>localhost:{item.port}</code><span>↗</span></div>
            </article>
          ))}
        </div>
      </section>

      <section className="playground" aria-labelledby="playground-title">
        <div className="playground-copy"><p className="eyebrow">FIRST CONNECTION</p><h2 id="playground-title">写下第一句，<br />让接口回应你。</h2><p className="muted">先在终端启动对应服务，再发起请求。<br />Java 打个招呼，Rust 算一组数字。</p><code className="endpoint">{service === 'rust' ? 'POST /api/rust/stats' : `GET /api/${service}/hello`}</code></div>
        <div className="console">
          <div className="tabs" role="tablist" aria-label="选择服务">{services.map((item) => <button key={item.id} id={`tab-${item.id}`} role="tab" aria-selected={service === item.id} aria-controls="request-panel" disabled={pending} className={service === item.id ? 'selected' : ''} onClick={() => { setService(item.id); setResult(null); setError('') }}>{item.id.toUpperCase()}</button>)}</div>
          <form id="request-panel" role="tabpanel" aria-labelledby={`tab-${service}`} onSubmit={run}>
            <label htmlFor="api-input">{service === 'rust' ? '用逗号或空格分隔的整数' : '怎么称呼你？'}</label>
            <div className="input-row"><input id="api-input" value={service === 'rust' ? numbers : name} maxLength={service === 'rust' ? 100000 : 50} onChange={(event) => service === 'rust' ? setNumbers(event.target.value) : setName(event.target.value)} required /><button className="send" disabled={pending}>{pending ? '请求中…' : '发送请求 →'}</button></div>
            <div className="response" aria-live="polite">{error ? <p className="error">{error}</p> : result ? <pre>{JSON.stringify(result, null, 2)}</pre> : <p className="placeholder">// 响应会出现在这里<br />// 每一个想法，都可以从一行代码开始。</p>}</div>
          </form>
        </div>
      </section>
      <footer><span>WITH YOUR HAND. WITH YOUR FRIEND.</span><span>保持好奇，持续动手。</span></footer>
    </main>
  )
}
