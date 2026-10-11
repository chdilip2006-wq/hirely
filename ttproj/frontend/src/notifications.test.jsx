import { beforeEach, describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import Notifications from './Notifications'
import { api } from './api'

vi.mock('./api',()=>({api:{get:vi.fn(),patch:vi.fn()},errorText:e=>e?.message||'Request failed'}))

describe('application notifications',()=>{
  beforeEach(()=>{api.get.mockResolvedValue({data:[{id:'notice-1',message:'Your application was updated.',read:false,createdAt:'2026-10-10T00:00:00Z'}]});api.patch.mockResolvedValue({})})
  it('lets a seeker mark an update as read',async()=>{
    render(<Notifications/>)
    expect(await screen.findByText('Your application was updated.')).toBeTruthy()
    fireEvent.click(screen.getByTitle('Mark as read'))
    await waitFor(()=>expect(api.patch).toHaveBeenCalledWith('/notifications/notice-1/read'))
  })
})
