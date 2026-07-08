import axios from 'axios'

const request = axios.create({ baseURL: '', timeout: 15000 })

request.interceptors.request.use(config => {
  const token = localStorage.getItem('memberToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  res => {
    if (res.data.code !== 200) {
      return Promise.reject(new Error(res.data.message))
    }
    return res.data
  },
  err => Promise.reject(err)
)

export default request
