import { useState } from 'react'
import { ArrowRight, BriefcaseBusiness, LockKeyhole, Sparkles } from 'lucide-react'
import { apiRequest, authStore } from '../lib/api'

export function AuthScreen({ onAuthenticated }) {
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const isRegister = mode === 'register'

  async function submit(event) {
    event.preventDefault(); setError(''); setBusy(true)
    try {
      const data = await apiRequest(`/auth/${mode}`, { method: 'POST', body: JSON.stringify(form) })
      authStore.save(data); onAuthenticated(data)
    } catch (err) { setError(err.message) } finally { setBusy(false) }
  }

  return <main className="auth-page">
    <section className="auth-art">
      <div className="brand auth-brand"><span className="brand-mark">HF</span><div><p className="brand-name">HireForge AI</p><p className="brand-tag">Your career, in motion.</p></div></div>
      <div className="auth-pitch"><span className="eyebrow"><Sparkles size={15}/> YOUR NEXT CHAPTER STARTS HERE</span><h1>Make your next move<br/><em>your best one.</em></h1><p>Bring your job search into focus. Track every opportunity, prepare with purpose, and move forward with confidence.</p><div className="auth-proof"><span><BriefcaseBusiness size={17}/> One clear pipeline</span><span><LockKeyhole size={17}/> Private by design</span></div></div>
      <div className="auth-art-foot">A little more clarity. A lot more momentum.</div>
    </section>
    <section className="auth-form-side"><form className="auth-card" onSubmit={submit}>
      <div className="auth-mobile-brand"><span className="brand-mark">HF</span><strong>HireForge AI</strong></div>
      <span className="eyebrow">{isRegister ? 'GET STARTED' : 'WELCOME BACK'}</span>
      <h2>{isRegister ? 'Create your account' : 'Sign in to your workspace'}</h2><p className="auth-muted">{isRegister ? 'A more focused job search begins here.' : 'Pick up where your job search left off.'}</p>
      {isRegister && <label>Full name<input autoComplete="name" value={form.name} onChange={e=>setForm({...form,name:e.target.value})} required maxLength={100} placeholder="Your name"/></label>}
      <label>Email address<input type="email" autoComplete="email" value={form.email} onChange={e=>setForm({...form,email:e.target.value})} required placeholder="you@example.com"/></label>
      <label>Password<input type="password" autoComplete={isRegister?'new-password':'current-password'} value={form.password} onChange={e=>setForm({...form,password:e.target.value})} required minLength={isRegister?8:1} placeholder={isRegister?'At least 8 characters':'Your password'}/></label>
      {error && <p className="auth-error" role="alert">{error}</p>}
      <button className="primary-button auth-submit" disabled={busy}>{busy ? 'Please wait…' : isRegister ? 'Create account' : 'Sign in'}<ArrowRight size={17}/></button>
      <p className="auth-switch">{isRegister ? 'Already have an account?' : 'New to HireForge?'} <button type="button" onClick={()=>{setMode(isRegister?'login':'register');setError('')}}>{isRegister?'Sign in':'Create an account'}</button></p>
      <p className="auth-privacy">Your applications are private to your account.</p>
    </form></section>
  </main>
}
