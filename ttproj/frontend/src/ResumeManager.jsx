import { useEffect, useState } from 'react'
import { api, errorText } from './api'
import { ArrowDownToLine, ArrowUpRight, FileText, ShieldCheck, Trash2 } from 'lucide-react'

export default function ResumeManager() {
  const [resume, setResume] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const load = () => api.get('/resume').then(r => setResume(r.data)).catch(e => {
    if (e.response?.status === 404) setResume(null)
    else setError(errorText(e))
  })
  useEffect(() => { load() }, [])

  const upload = async e => {
    const file = e.target.files?.[0]
    if (!file) return
    const form = new FormData()
    form.append('file', file)
    setBusy(true); setError('')
    try { setResume((await api.post('/resume', form)).data) }
    catch (err) { setError(errorText(err)) }
    finally { setBusy(false); e.target.value = '' }
  }
  const openResume = async download => {
    const popup = download ? null : window.open('about:blank', '_blank')
    setError('')
    try {
      const response = await api.get(download ? '/resume/download' : '/resume/view', { responseType: 'blob' })
      const url = URL.createObjectURL(response.data)
      if (download) {
        const link = document.createElement('a')
        link.href = url; link.download = resume?.originalName || 'resume'; link.click()
      } else if (popup) popup.location.href = url
      else window.location.href = url
      setTimeout(() => URL.revokeObjectURL(url), 60_000)
    } catch (err) { popup?.close(); setError(errorText(err)) }
  }
  const remove = async () => {
    setError('')
    try { await api.delete('/resume'); setResume(null) }
    catch (err) { setError(errorText(err)) }
  }

  return <aside className="panel resume-panel">
    <div className="resume-icon"><FileText/></div><h3>Your resume</h3>
    <p>PDF, DOC or DOCX, up to 5 MB. Recruiters can access it after you apply.</p>
    {error && <div className="alert">{error}</div>}
    {resume && <div className="resume-metadata"><strong>{resume.originalName}</strong><span>{Math.max(1, Math.round(resume.sizeBytes / 1024))} KB · uploaded {new Date(resume.uploadedAt).toLocaleDateString()}</span><div><button className="text-link" onClick={() => openResume(false)}><ArrowUpRight size={14}/> View</button><button className="text-link" onClick={() => openResume(true)}><ArrowDownToLine size={14}/> Download</button><button className="text-link resume-delete" onClick={remove}><Trash2 size={13}/> Remove</button></div></div>}
    <label className="button button-outline full">{busy ? 'Uploading…' : resume ? 'Replace resume' : 'Upload a resume'}<input type="file" accept=".pdf,.doc,.docx" onChange={upload} disabled={busy}/></label>
    <div className="privacy-note"><ShieldCheck size={16}/> Only you and recruiters whose roles you apply to can access your resume.</div>
  </aside>
}
