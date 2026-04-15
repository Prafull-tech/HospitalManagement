const LOGO_MAX_BYTES = 300 * 1024

export function normalizeLogoSrc(value: string | null | undefined): string {
  const raw = (value ?? '').trim()
  if (!raw) return ''

  const lower = raw.toLowerCase()
  if (
    lower.startsWith('http://') ||
    lower.startsWith('https://') ||
    lower.startsWith('data:image/') ||
    lower.startsWith('blob:') ||
    lower.startsWith('/')
  ) {
    return raw
  }

  return `https://${raw}`
}

export async function fileToLogoDataUrl(file: File): Promise<string> {
  if (!file.type.startsWith('image/')) {
    throw new Error('Please choose an image file.')
  }

  if (file.size > LOGO_MAX_BYTES) {
    throw new Error('Image is too large. Keep logo files under 300 KB.')
  }

  return await new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : ''
      if (!result.startsWith('data:image/')) {
        reject(new Error('Selected file is not a supported image.'))
        return
      }
      resolve(result)
    }
    reader.onerror = () => reject(new Error('Failed to read selected image file.'))
    reader.readAsDataURL(file)
  })
}