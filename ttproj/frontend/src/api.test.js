import { describe, expect, it } from 'vitest'
import { errorText } from './api'

describe('API error messages', () => {
  it('uses the backend message when one is available', () => {
    expect(errorText({ response: { data: { message: 'Already applied.' } } })).toBe('Already applied.')
  })
  it('provides a useful fallback for unknown errors', () => {
    expect(errorText({})).toContain('Something went wrong')
  })
})
