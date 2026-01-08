import { createRouter, createWebHistory } from 'vue-router'

// 引入刚才创建的页面组件
import Login from '../views/Login.vue'
import UserHome from '../views/UserHome.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import AdminUserManagement from '../views/AdminUserManagement.vue'
import AdminDoctorManagement from '../views/AdminDoctorManagement.vue'
import AdminOrderManagement from '../views/AdminOrderManagement.vue'
import AdminScheduleManagement from '../views/AdminScheduleManagement.vue'
import AdminCampusDepartmentManagement from '../views/AdminCampusDepartmentManagement.vue'
import AdminAuditLogManagement from '../views/AdminAuditLogManagement.vue'
import DoctorDashboard from '../views/DoctorDashboard.vue'
import DoctorOverview from '../views/DoctorOverview.vue'
import DoctorScheduleManagement from '../views/DoctorScheduleManagement.vue'
import DoctorMedicalRecords from '../views/DoctorMedicalRecords.vue'
import DoctorProfile from '../views/DoctorProfile.vue'
import HospitalIntro from '../views/HospitalIntro.vue'
import CampusDetail from '../views/CampusDetail.vue'
import OutpatientSchedule from '../views/OutpatientSchedule.vue'
import SpecialistList from '../views/SpecialistList.vue'
import DepartmentNavigation from '../views/DepartmentNavigation.vue'
import DepartmentDetail from '../views/DepartmentDetail.vue'
import AppointmentRegister from '../views/AppointmentRegister.vue'
import ContactUs from '../views/ContactUs.vue'
import MyPatients from '../views/MyPatients.vue'
import MyProfile from '../views/MyProfile.vue'
import MyReports from '../views/MyReports.vue'
import VisitRecords from '../views/VisitRecords.vue'

const routes = [
  {
    path: '/',
    redirect: '/login' // 默认一进来就跳到登录页
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/user',
    name: 'UserHome',
    component: UserHome
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresRole: 'admin' },
    children: [
      { path: '', redirect: { name: 'AdminUsers' } },
      { path: 'users', name: 'AdminUsers', component: AdminUserManagement, meta: { title: '用户管理' } },
      { path: 'doctors', name: 'AdminDoctors', component: AdminDoctorManagement, meta: { title: '医生管理' } },
      { path: 'orders', name: 'AdminOrders', component: AdminOrderManagement, meta: { title: '预约订单管理' } },
      { path: 'schedule', name: 'AdminSchedule', component: AdminScheduleManagement, meta: { title: '排班管理' } },
      { path: 'campus', name: 'AdminCampus', component: AdminCampusDepartmentManagement, meta: { title: '院区与科室' } },
      { path: 'logs', name: 'AdminLogs', component: AdminAuditLogManagement, meta: { title: '日志与审计' } },
    ]
  },
  // ✅ 医生端：Layout + 子路由
  {
    path: '/doctor',
    name: 'DoctorDashboard',
    component: DoctorDashboard,
    meta: { requiresRole: 'doctor' },
    children: [
      { path: '', redirect: { name: 'DoctorOverview' } },
      { path: 'dashboard', name: 'DoctorOverview', component: DoctorOverview, meta: { title: '今日接诊' } },
      { path: 'schedule', name: 'DoctorSchedule', component: DoctorScheduleManagement, meta: { title: '我的排班' } },
      { path: 'records', name: 'DoctorRecords', component: DoctorMedicalRecords, meta: { title: '病历档案' } },
      { path: 'profile', name: 'DoctorProfile', component: DoctorProfile, meta: { title: '个人信息' } },
    ]
  },
  // 2. 添加这条新路由
  {
    path: '/intro',         // 浏览器地址栏会显示这个
    name: 'HospitalIntro',
    component: HospitalIntro // 对应显示这个组件
  },
  // 新增的动态路由
  {
    path: '/campus/:id', 
    name: 'CampusDetail',
    component: CampusDetail // 这里使用了 CampusDetail，所以上面必须 import
  },
  // 2. 添加排班路由
  {
    path: '/schedule',
    name: 'OutpatientSchedule',
    component: OutpatientSchedule
  },
  {
    path: '/specialist',
    name: 'SpecialistList',
    component: SpecialistList
  },
  {
    path: '/department',
    name: 'DepartmentNavigation',
    component: DepartmentNavigation
  },
  {
    path: '/department/:id',
    name: 'DepartmentDetail',
    component: DepartmentDetail
  },
  {
    path: '/appointment',
    name: 'AppointmentRegister',
    component: AppointmentRegister
  },
  {
    path: '/contact',
    name: 'ContactUs',
    component: ContactUs
  },
  {
    path: '/patients',
    name: 'MyPatients',
    component: MyPatients
  },
  {
    path: '/profile',
    name: 'MyProfile',
    component: MyProfile
  },
  {
    path: '/reports',
    name: 'MyReports',
    component: MyReports
  },
  {
    path: '/visit-records',
    name: 'VisitRecords',
    component: VisitRecords
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router