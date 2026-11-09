import { useState } from 'react'
import {
  AlertTriangle,
  BadgeCheck,
  BriefcaseBusiness,
  CheckCircle2,
  FileText,
  MessageCircle,
  Send,
  Sparkles,
  Target,
  UploadCloud,
} from 'lucide-react'
import { apiRequest } from '../lib/api'

function ToolHeading({ icon: Icon, title, copy }) {
  return (
    <div className="tool-heading">
      <span className="tool-icon"><Icon size={20} /></span>
      <div><h2>{title}</h2><p>{copy}</p></div>
    </div>
  )
}

function MessageState({ error }) {
  if (!error) return null
  const message = typeof error === 'string' ? error : error.message
  const rateLimited = error?.status === 429
  const wait = error?.retryAfter ? ` Try again in ${error.retryAfter} seconds.` : ''
  return <div className={`ai-error ${rateLimited ? 'ai-rate-limit' : ''}`} role="alert">
    <AlertTriangle size={18} />
    <span>{rateLimited && <strong>Groq rate limit reached. </strong>}{message}{rateLimited ? wait : ''}</span>
  </div>
}

function LoadingState({ label }) {
  return <div className="ai-loading" role="status"><span className="ai-spinner" /><div><strong>{label}</strong><p>Groq is reviewing the information you provided.</p></div></div>
}

function ResultList({ title, items, icon: Icon = CheckCircle2, tone = '' }) {
  return <section className={`ai-result-card ${tone}`}>
    <h3><Icon size={17} />{title}</h3>
    {items?.length ? <ul>{items.map((item, index) => <li key={`${item}-${index}`}>{item}</li>)}</ul> : <p className="ai-muted">No items identified.</p>}
  </section>
}

export function ResumeReview() {
  const [file, setFile] = useState(null)
  const [role, setRole] = useState('')
  const [result, setResult] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function analyze(event) {
    event.preventDefault()
    if (!file) return
    setBusy(true)
    setError('')
    setResult(null)
    try {
      const body = new FormData()
      body.append('file', file)
      body.append('role', role)
      setResult(await apiRequest('/career/resume-analysis', { method: 'POST', body }))
    } catch (requestError) {
      setError(requestError)
    } finally {
      setBusy(false)
    }
  }

  return <section className="panel tool-panel">
    <ToolHeading icon={FileText} title="AI resume analyzer" copy="Get evidence-based feedback on clarity, role fit, and ATS readiness." />
    <form onSubmit={analyze} className="tool-form ai-tool-form">
      <label className="upload-box">
        <UploadCloud size={24} />
        <strong>{file?.name || 'Choose a resume PDF'}</strong>
        <span>PDF only · up to 5 MB</span>
        <input type="file" accept="application/pdf,.pdf" onChange={event => { setFile(event.target.files?.[0] || null); setResult(null); setError('') }} />
      </label>
      <label>Target role<input value={role} onChange={event => setRole(event.target.value)} maxLength={150} required placeholder="e.g. Product Designer" /></label>
      <button className="primary-button" disabled={!file || busy}><Sparkles size={16} />{busy ? 'Analyzing resume…' : 'Analyze resume'}</button>
    </form>
    <p className="ai-privacy-note">Your PDF is sent to the configured Groq model for analysis and is not saved by HireForge.</p>
    <MessageState error={error} />
    {busy && <LoadingState label="Building your resume review…" />}
    {result && <div className="ai-results" aria-live="polite">
      <div className="ai-score-summary">
        <div className="ai-score"><strong>{result.overallScore}</strong><span>/ 100</span></div>
        <div><span className="ai-kicker">Resume review</span><h3>Role fit snapshot</h3><p>{result.summary}</p></div>
      </div>
      <div className="ai-result-grid">
        <ResultList title="Strengths" items={result.strengths} />
        <ResultList title="Areas to improve" items={result.weaknesses} icon={Target} tone="ai-result-warm" />
        <ResultList title="Skills not evidenced" items={result.missingSkills} icon={BriefcaseBusiness} tone="ai-result-warm" />
        <ResultList title="ATS suggestions" items={result.atsSuggestions} icon={BadgeCheck} />
        <ResultList title="Actionable improvements" items={result.actionableImprovements} icon={Sparkles} />
      </div>
      <p className="ai-disclaimer">Suggestions are based on the supplied resume. Only add details you can verify.</p>
    </div>}
  </section>
}

export function JobMatcher() {
  const [form, setForm] = useState({ resumeText: '', jobDescription: '' })
  const [result, setResult] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  function update(field, value) { setForm(current => ({ ...current, [field]: value })) }

  async function match(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setResult(null)
    try {
      setResult(await apiRequest('/career/job-match', { method: 'POST', body: JSON.stringify(form) }))
    } catch (requestError) {
      setError(requestError)
    } finally {
      setBusy(false)
    }
  }

  return <section className="panel tool-panel">
    <ToolHeading icon={Target} title="Job description matcher" copy="Compare a resume with a role and find the strongest honest ways to improve your application." />
    <form className="tool-form ai-match-form" onSubmit={match}>
      <label>Resume text<textarea rows={9} maxLength={18000} value={form.resumeText} onChange={event => update('resumeText', event.target.value)} required placeholder="Paste resume text here…" /></label>
      <label>Job description<textarea rows={9} maxLength={14000} value={form.jobDescription} onChange={event => update('jobDescription', event.target.value)} required placeholder="Paste the job description here…" /></label>
      <button className="primary-button" disabled={busy}><Sparkles size={16} />{busy ? 'Comparing…' : 'Compare resume and role'}</button>
    </form>
    <p className="ai-privacy-note">Text is sent to Groq for this analysis and is not saved by HireForge.</p>
    <MessageState error={error} />
    {busy && <LoadingState label="Checking skills and experience alignment…" />}
    {result && <div className="ai-results" aria-live="polite">
      <div className="ai-score-summary">
        <div className="ai-score"><strong>{result.matchScore}</strong><span>/ 100</span></div>
        <div><span className="ai-kicker">Resume and job comparison</span><h3>Match overview</h3><p>{result.summary}</p></div>
      </div>
      <section className="ai-result-card"><h3><CheckCircle2 size={17} />Evidence-backed overlaps</h3>
        {result.matchingSkills?.length ? <div className="match-skill-list">{result.matchingSkills.map((item, index) => <article key={`${item.skill}-${index}`}><strong>{item.skill}</strong><p><b>Resume:</b> {item.resumeEvidence}</p><p><b>Role:</b> {item.jobRequirement}</p></article>)}</div> : <p className="ai-muted">No direct overlaps were identified in the supplied text.</p>}
      </section>
      <div className="ai-result-grid">
        <ResultList title="Keywords not evidenced" items={result.missingKeywords} icon={Target} tone="ai-result-warm" />
        <section className="ai-result-card"><h3><BriefcaseBusiness size={17} />Experience alignment</h3><p>{result.experienceAlignment}</p></section>
        <ResultList title="Recommendations" items={result.recommendations} icon={Sparkles} />
      </div>
      <p className="ai-disclaimer">A match score is an estimate from the supplied text, not a hiring prediction.</p>
    </div>}
  </section>
}

export function InterviewPractice() {
  const [form, setForm] = useState({ role: '', company: '', jobDescription: '' })
  const [result, setResult] = useState(null)
  const [answers, setAnswers] = useState({})
  const [evaluations, setEvaluations] = useState({})
  const [busy, setBusy] = useState(false)
  const [busyQuestion, setBusyQuestion] = useState('')
  const [error, setError] = useState('')

  function update(field, value) { setForm(current => ({ ...current, [field]: value })) }

  async function prepare(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setEvaluations({})
    try {
      const data = await apiRequest('/career/interview-prep', { method: 'POST', body: JSON.stringify(form) })
      setResult(data)
      setAnswers({})
    } catch (requestError) {
      setError(requestError)
    } finally {
      setBusy(false)
    }
  }

  async function evaluate(question) {
    const answer = answers[question.id]?.trim()
    if (!answer) return
    setBusyQuestion(question.id)
    setError('')
    try {
      const evaluation = await apiRequest('/career/interview-evaluate', {
        method: 'POST',
        body: JSON.stringify({ role: form.role, company: form.company, question: question.question, answer }),
      })
      setEvaluations(current => ({ ...current, [question.id]: evaluation }))
    } catch (requestError) {
      setError(requestError)
    } finally {
      setBusyQuestion('')
    }
  }

  return <section className="panel tool-panel">
    <ToolHeading icon={MessageCircle} title="AI interview coach" copy="Get role-specific questions, practice your answers, and receive useful coaching." />
    <form className="tool-form interview-form ai-interview-form" onSubmit={prepare}>
      <label>Target role<input value={form.role} onChange={event => update('role', event.target.value)} maxLength={150} required placeholder="e.g. Backend Engineer" /></label>
      <label>Company<input value={form.company} onChange={event => update('company', event.target.value)} maxLength={150} placeholder="Optional" /></label>
      <label className="full-width">Job description<textarea rows={4} maxLength={14000} value={form.jobDescription} onChange={event => update('jobDescription', event.target.value)} placeholder="Paste the role description for tailored questions…" /></label>
      <button className="primary-button" disabled={busy}><Sparkles size={16} />{busy ? 'Preparing questions…' : 'Generate practice questions'}</button>
    </form>
    <MessageState error={error} />
    {busy && <LoadingState label="Creating your practice set…" />}
    {result && <div className="question-list ai-question-list" aria-live="polite">
      <div className="ai-section-heading"><div><span className="ai-kicker">Practice set</span><h3>{result.questions?.length || 0} questions for {form.role}</h3></div><p>{result.preparationNote}</p></div>
      {result.questions?.map((question, index) => <article className={`ai-question-card ${evaluations[question.id] ? 'has-feedback' : ''}`} key={question.id}>
        <div className="ai-question-main">
          <div className="ai-question-top"><span className="ai-question-number">{String(index + 1).padStart(2, '0')}</span><span className="ai-question-category">{question.category}</span></div>
          <h4>{question.question}</h4><p className="ai-muted">Assesses: {question.whatItAssesses}</p>
          <label className="answer-field">Your answer<textarea rows={7} maxLength={6000} value={answers[question.id] || ''} onChange={event => setAnswers(current => ({ ...current, [question.id]: event.target.value }))} placeholder="Write your answer here…" /></label>
          <button type="button" className="secondary-button ai-evaluate-button" disabled={!answers[question.id]?.trim() || busyQuestion === question.id} onClick={() => evaluate(question)}><Send size={15} />{busyQuestion === question.id ? 'Reviewing answer…' : 'Get feedback'}</button>
        </div>
        {evaluations[question.id] && <AnswerFeedback evaluation={evaluations[question.id]} />}
      </article>)}
      <p className="ai-disclaimer">Your answers are sent to Groq for feedback and are not saved by HireForge. Sample answers use only details you supplied.</p>
    </div>}
  </section>
}

function AnswerFeedback({ evaluation }) {
  return <div className="answer-feedback">
    <div className="answer-score"><strong>{evaluation.score}</strong><span>/100</span></div>
    <div className="answer-feedback-copy"><h4>Coach feedback</h4><p>{evaluation.overallFeedback}</p></div>
    <div className="ai-result-grid">
      <ResultList title="What worked" items={evaluation.strengths} />
      <ResultList title="Improve next" items={evaluation.improvementAreas} icon={Target} tone="ai-result-warm" />
    </div>
    <section className="sample-answer"><h4><Sparkles size={16} />A stronger version</h4><p>{evaluation.sampleAnswer}</p></section>
  </div>
}
