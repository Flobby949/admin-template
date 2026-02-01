declare module '@wangeditor/editor-for-vue' {
  import { DefineComponent } from 'vue'
  import type { IDomEditor, IToolbarConfig, IEditorConfig } from '@wangeditor/editor'

  export const Editor: DefineComponent<{
    modelValue?: string
    defaultConfig?: Partial<IEditorConfig>
    mode?: 'default' | 'simple'
  }>

  export const Toolbar: DefineComponent<{
    editor?: IDomEditor
    defaultConfig?: Partial<IToolbarConfig>
    mode?: 'default' | 'simple'
  }>
}
