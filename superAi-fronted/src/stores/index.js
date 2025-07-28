import { createPinia } from 'pinia'
import { useAuthStore } from './auth'
import { useChatStore } from './chat'
import { useProcessingStore } from './processing'

const pinia = createPinia()

export default pinia

export {
  useAuthStore,
  useChatStore,
  useProcessingStore
}