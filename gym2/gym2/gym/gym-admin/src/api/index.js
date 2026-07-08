import request from './request'

// ========== 认证 ==========
export const memberLogin = (data) => request.post('/api/v1/auth/login', data)
export const memberRegister = (data) => request.post('/api/v1/auth/register', data)
export const adminLogin = (data) => request.post('/api/v1/admin/login', data)

// ========== 会员 ==========
export const getMembers = (params) => request.get('/api/v1/members', { params })
export const getMemberDetail = (id) => request.get(`/api/v1/members/${id}`)
export const updateMember = (id, data) => request.put(`/api/v1/members/${id}`, data)
export const updateMemberStatus = (id, status) => request.put(`/api/v1/members/${id}/status?status=${status}`)

// ========== 教练 ==========
export const getCoaches = (params) => request.get('/api/v1/coaches', { params })
export const getAllCoaches = () => request.get('/api/v1/coaches/all')
export const addCoach = (data) => request.post('/api/v1/coaches', data)
export const updateCoach = (id, data) => request.put(`/api/v1/coaches/${id}`, data)
export const deleteCoach = (id) => request.delete(`/api/v1/coaches/${id}`)

// ========== 课程 ==========
export const getCourses = (params) => request.get('/api/v1/courses', { params })
export const addCourse = (data) => request.post('/api/v1/courses', data)
export const updateCourse = (id, data) => request.put(`/api/v1/courses/${id}`, data)
export const deleteCourse = (id) => request.delete(`/api/v1/courses/${id}`)

// ========== 排课 ==========
export const getSchedules = (params) => request.get('/api/v1/schedules', { params })
export const getAvailableSchedules = () => request.get('/api/v1/schedules/available')
export const addSchedule = (data) => request.post('/api/v1/schedules', data)
export const updateSchedule = (id, data) => request.put(`/api/v1/schedules/${id}`, data)
export const cancelSchedule = (id) => request.put(`/api/v1/schedules/${id}/cancel`)
export const cleanExpiredSchedules = () => request.delete('/api/v1/schedules/clean-expired')

// ========== 预约 ==========
export const bookReservation = (data) => request.post('/api/v1/reservations', data)
export const cancelReservation = (id, reason) => request.delete(`/api/v1/reservations/${id}?reason=${reason || '用户取消'}`)
export const checkinReservation = (id) => request.put(`/api/v1/reservations/${id}/checkin`)
export const getMyReservations = () => request.get('/api/v1/reservations/my')
export const getAllReservations = (status) => request.get('/api/v1/reservations/all', { params: { status } })

// ========== 订单 ==========
export const createOrder = (data) => request.post('/api/v1/orders', data)
export const getMyOrders = (params) => request.get('/api/v1/orders/my', { params })
export const getAllOrders = (params) => request.get('/api/v1/orders', { params })
export const cancelOrder = (id) => request.put(`/api/v1/orders/${id}/cancel`)

// ========== 支付 ==========
export const payOrder = (data) => request.post('/api/v1/payments', data)
export const refundOrder = (orderId) => request.post(`/api/v1/payments/${orderId}/refund`)

// ========== 会员卡 ==========
export const getMyCards = () => request.get('/api/v1/cards/my')
export const buyCard = (cardType) => request.post(`/api/v1/cards?cardType=${cardType}`)
export const renewCard = (id) => request.post(`/api/v1/cards/${id}/renew`)

// ========== 器材 ==========
export const getEquipment = (params) => request.get('/api/v1/equipment', { params })
export const addEquipment = (data) => request.post('/api/v1/equipment', data)
export const updateEquipment = (id, data) => request.put(`/api/v1/equipment/${id}`, data)
export const updateEquipmentStatus = (id, status) => request.put(`/api/v1/equipment/${id}/status?status=${status}`)
export const deleteEquipment = (id) => request.delete(`/api/v1/equipment/${id}`)

// ========== 报修 ==========
export const getRepairOrders = (params) => request.get('/api/v1/repair-orders', { params })
export const reportRepair = (data) => request.post('/api/v1/repair-orders', data)
export const startRepair = (id, person) => request.put(`/api/v1/repair-orders/${id}/start?repairPerson=${person}`)
export const completeRepair = (id, cost) => request.put(`/api/v1/repair-orders/${id}/complete?cost=${cost || 0}`)

// ========== 员工 ==========
export const getEmployees = (params) => request.get('/api/v1/employees', { params })
export const addEmployee = (data) => request.post('/api/v1/employees', data)
export const updateEmployee = (id, data) => request.put(`/api/v1/employees/${id}`, data)
export const updateEmployeeStatus = (id, status) => request.put(`/api/v1/employees/${id}/status?status=${status}`)

// ========== 考勤 ==========
export const checkIn = () => request.post('/api/v1/attendance/check-in')
export const checkOut = (id) => request.put(`/api/v1/attendance/${id}/check-out`)
export const getAttendance = (params) => request.get('/api/v1/attendance', { params })

// ========== 统计 ==========
export const getDashboardStats = () => request.get('/api/v1/dashboard/stats')

// ========== AI ==========
export const aiChat = (data) => request.post('/api/v1/ai/chat', data)
export const aiRecommend = () => request.post('/api/v1/ai/recommend')

// ========== 通用 ==========
export const uploadFile = (formData) => request.post('/api/v1/common/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
