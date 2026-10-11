import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowRight, BriefcaseBusiness, Building2, Check, MapPin } from 'lucide-react'
import { api, errorText } from './api'
import { useAuth } from './auth'
import ApplyDialog from './ApplyDialog'

export default function JobApplicationPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [job, setJob] = useState(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [applied, setApplied] = useState(false)
  const [showApply, setShowApply] = useState(false)

  useEffect(() => {
    setJob(null); setError(''); setSuccess(''); setApplied(false)
    api.get(`/jobs/${id}`).then(r => setJob(r.data)).catch(e => setError(errorText(e)))
  }, [id])
  useEffect(() => {
    if (user?.role === 'JOB_SEEKER') api.get('/applications').then(r => setApplied(r.data.some(app => app.jobId === id))).catch(() => {})
  }, [id, user?.id, user?.role])

  const openApply = () => {
    if (!user) { navigate('/login'); return }
    if (user.role !== 'JOB_SEEKER') { setError('Only job seeker accounts can apply to a role.'); return }
    setError(''); setSuccess(''); setShowApply(true)
  }
  const submitted = result => {
    setApplied(true)
    setSuccess(result.message || 'Your application was submitted successfully.')
    setShowApply(false)
  }

  if (error && !job) return <main className="page"><div className="alert">{error}</div></main>
  if (!job) return <div className="loading-screen"><span className="spinner"/>Opening this role…</div>
  return <main className="page detail-page">
    <Link to="/jobs" className="back-link">← All opportunities</Link>
    <div className="detail-top">
      <div className="company-logo large">{job.company?.[0]}</div>
      <div><div className="eyebrow muted"><span className="eyebrow-dot"/> ROLE AT {job.company?.toUpperCase()}</div><h1>{job.title}</h1><div className="detail-sub"><span><Building2 size={16}/>{job.company}</span><span><MapPin size={16}/>{job.location}</span><span><BriefcaseBusiness size={16}/>{job.jobType?.replaceAll('_', ' ')}</span></div></div>
      <button className="button button-dark apply-button" onClick={openApply} disabled={applied}>{applied ? 'Application received' : 'Apply for this role'} {applied ? <Check size={16}/> : <ArrowRight size={16}/>}</button>
    </div>
    {error && <div className="alert" role="alert">{error}</div>}
    {success && <div className="success apply-success" role="status">{success} <Link to="/dashboard">View your applications</Link></div>}
    <div className="detail-layout">
      <article className="detail-content"><section><h2>The role</h2><p className="long-copy">{job.description}</p></section>{job.requirements && <section><h2>What you’ll bring</h2><p className="long-copy">{job.requirements}</p></section>}<section><h2>A little about {job.company}</h2><p className="long-copy">{job.companyDescription || job.industry || 'A team building what’s next.'}</p></section></article>
      <aside className="detail-aside"><h3>At a glance</h3><div className="aside-row"><span>Location</span><strong>{job.location}</strong></div><div className="aside-row"><span>Work style</span><strong>{job.workplaceType?.replaceAll('_', ' ')}</strong></div><div className="aside-row"><span>Experience</span><strong>{job.experienceLevel || 'All levels'}</strong></div><div className="aside-row"><span>Salary</span><strong>{job.salaryMin ? `₹${Number(job.salaryMin).toLocaleString('en-IN')}` : 'Competitive'}</strong></div><button className="button button-dark full" onClick={openApply} disabled={applied}>{applied ? 'Application received' : 'Apply now'} <ArrowRight size={15}/></button></aside>
    </div>
    {showApply && <ApplyDialog job={job} user={user} onClose={() => setShowApply(false)} onSubmitted={submitted}/>}
  </main>
}
