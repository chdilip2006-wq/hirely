import { useEffect, useState } from 'react'
import { ArrowDownToLine, ArrowUpRight, Check, FileText, Upload, X } from 'lucide-react'
import { api, errorText } from './api'

const validPhone = value => {
  const digits = (value || '').replace(/\D/g, '')
  return /^\+?[0-9][0-9\s().-]{5,29}$/.test(value || '') && digits.length >= 7 && digits.length <= 15
}

export default function ApplyDialog({ job, user, onClose, onSubmitted }) {
  const [form, setForm] = useState({ fullName: user?.fullName || '', email: user?.email || '', phone: user?.phone || '', coverLetter: '' })
  const [resume, setResume] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [uploading, setUploading] = useState(false)

  useEffect(() => {
    api.get('/resume').then(r => setResume(r.data)).catch(e => { if (e.response?.status !== 404) setError(errorText(e)) })
  }, [])
  useEffect(() => {
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    const closeOnEscape = event => { if (event.key === 'Escape') onClose() }
    window.addEventListener('keydown', closeOnEscape)
    return () => { document.body.style.overflow = previousOverflow; window.removeEventListener('keydown', closeOnEscape) }
  }, [onClose])

  const update = e => setForm(current => ({ ...current, [e.target.name]: e.target.value }))
  const upload = async e => {
    const file = e.target.files?.[0]
    if (!file) return
    const data = new FormData()
    data.append('file', file)
    setUploading(true)
    setError('')
    try { setResume((await api.post('/resume', data)).data) }
    catch (err) { setError(errorText(err)) }
    finally { setUploading(false); e.target.value = '' }
  }
  const openResume = async download => {
    const popup = download ? null : window.open('about:blank', '_blank')
    try {
      const result = await api.get(download ? '/resume/download' : '/resume/view', { responseType: 'blob' })
      const url = URL.createObjectURL(result.data)
      if (download) {
        const link = document.createElement('a')
        link.href = url
        link.download = resume?.originalName || 'resume'
        link.click()
      } else if (popup) popup.location.href = url
      else window.location.href = url
      setTimeout(() => URL.revokeObjectURL(url), 60_000)
    } catch (err) { popup?.close(); setError(errorText(err)) }
  }
  const submit = async e => {
    e.preventDefault()
    setError('')
    if (!resume) { setError('Upload your resume before applying.'); return }
    if (!validPhone(form.phone)) { setError('Enter a valid phone number with 7 to 15 digits. Include your country code if applicable.'); return }
    setBusy(true)
    try {
      const { data } = await api.post(`/jobs/${job.id}/apply`, form)
      onSubmitted(data)
    } catch (err) { setError(errorText(err)) }
    finally { setBusy(false) }
  }

  return <div className="apply-modal-backdrop" onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}>
    <section className="apply-modal" role="dialog" aria-modal="true" aria-labelledby="apply-title">
      <header className="apply-modal-heading"><div><span className="eyebrow muted">APPLICATION</span><h2 id="apply-title">Apply for {job.title}</h2><p>{job.company} · {job.location}</p></div><button type="button" className="modal-close" onClick={onClose} aria-label="Close application"><X size={19}/></button></header>
      {error && <div className="alert" role="alert">{error}</div>}
      <form onSubmit={submit}>
        <div className="apply-contact-grid">
          <label className="field">Full name<input name="fullName" required maxLength="120" autoComplete="name" autoFocus value={form.fullName} onChange={update}/></label>
          <label className="field">Contact email<input name="email" required type="email" autoComplete="email" value={form.email} onChange={update}/></label>
        </div>
        <label className="field">Phone number<input name="phone" required type="tel" autoComplete="tel" inputMode="tel" placeholder="+1 202 555 0123" value={form.phone} onChange={update}/><span className="field-hint">Use your country code. Spaces, +, parentheses, dots and hyphens are accepted.</span></label>
        <div className="apply-resume-box"><div className="apply-resume-icon"><FileText size={19}/></div><div className="apply-resume-copy"><strong>{resume?.originalName || 'Resume required'}</strong><span>{resume ? `${Math.max(1, Math.round(resume.sizeBytes / 1024))} KB · attached to this application` : 'PDF, DOC or DOCX up to 5 MB'}</span></div>
          {resume && <div className="apply-resume-actions"><button type="button" className="text-link" onClick={() => openResume(false)}><ArrowUpRight size={14}/> View</button><button type="button" className="text-link" onClick={() => openResume(true)}><ArrowDownToLine size={14}/> Download</button></div>}
          <label className="button button-outline button-small apply-upload">{uploading ? 'Uploading…' : resume ? 'Replace' : <><Upload size={14}/> Upload</>}<input type="file" accept=".pdf,.doc,.docx" onChange={upload} disabled={uploading}/></label>
        </div>
        <label className="field">Cover letter <span className="field-hint">Optional</span><textarea name="coverLetter" rows="4" maxLength="5000" placeholder="Share a few words about why this role interests you." value={form.coverLetter} onChange={update}/></label>
        <p className="apply-privacy">Your contact details and resume will be shared with this employer for this application.</p>
        <div className="apply-modal-footer"><button type="button" className="button button-outline" onClick={onClose}>Cancel</button><button type="submit" className="button button-dark" disabled={busy || uploading || !resume}>{busy ? 'Submitting…' : <>Submit application <Check size={15}/></>}</button></div>
      </form>
    </section>
  </div>
}
