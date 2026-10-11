import { beforeEach, describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import SavedRoles from './SavedRoles'
import { api } from './api'

vi.mock('./api',()=>({api:{get:vi.fn(),delete:vi.fn()},errorText:e=>e?.message||'Request failed'}))

describe('saved roles',()=>{
  beforeEach(()=>{api.get.mockResolvedValue({data:[{id:'job-1',title:'Product designer',company:'Northstar',location:'Remote'}]});api.delete.mockResolvedValue({})})
  it('shows saved opportunities and removes one from the list',async()=>{
    render(<BrowserRouter><SavedRoles/></BrowserRouter>)
    expect(await screen.findByText('Product designer')).toBeTruthy()
    fireEvent.click(screen.getByTitle('Remove saved role'))
    await waitFor(()=>expect(api.delete).toHaveBeenCalledWith('/jobs/job-1/save'))
  })
})
