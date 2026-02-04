<template>
  <div />
</template>

<script setup lang="ts">
import { onBeforeMount } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

onBeforeMount(() => {
  const { params } = route
  const { path } = params
  const redirectPath = Array.isArray(path) ? path.join('/') : path
  router.replace('/' + redirectPath)
})
</script>
