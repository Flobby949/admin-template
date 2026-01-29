import { defineStore } from 'pinia'
import { getDicts, DictDataVO } from '@/api/system/dict/data'

export const useDictStore = defineStore('dict', {
  state: () => ({
    dict: new Map<string, DictDataVO[]>()
  }),
  actions: {
    // 获取字典
    async getDict(dictType: string): Promise<DictDataVO[]> {
      if (!dictType) {
        return []
      }
      if (!this.dict.has(dictType)) {
        try {
          const res = await getDicts(dictType)
          this.dict.set(dictType, res || []) // Assume res is the array directly or res.data, checked api definition, it returns Promise<DictDataVO[]> which usually means request<T> returns T directly or Response<T>.
          // Looking at request.ts, `service.interceptors.response.use` returns `res.data` (if code is 200). 
          // Wait, `request<T>` in `frontend/src/api/role.ts` returns `Promise<T>`. 
          // `getDicts` returns `request<DictDataVO[]>`. 
          // So `res` is `DictDataVO[]`.
        } catch (error) {
          console.error(`Get dict ${dictType} failed:`, error)
          return []
        }
      }
      return this.dict.get(dictType) || []
    },

    // 删除字典
    removeDict(dictType: string) {
      if (this.dict.has(dictType)) {
        this.dict.delete(dictType)
      }
    },

    // 清空字典
    cleanDict() {
      this.dict.clear()
    }
  }
})

export default useDictStore
