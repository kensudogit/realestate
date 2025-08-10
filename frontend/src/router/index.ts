import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import PropertyList from '@/views/PropertyList.vue'
import PropertyDetail from '@/views/PropertyDetail.vue'
import PropertyEdit from '@/views/PropertyEdit.vue'
import ClientList from '@/views/ClientList.vue'
import ClientEdit from '@/views/ClientEdit.vue'
import ContractList from '@/views/ContractList.vue'
import ContractDetail from '@/views/ContractDetail.vue'
import ContractEdit from '@/views/ContractEdit.vue'
import TransactionList from '@/views/TransactionList.vue'
import TransactionDetail from '@/views/TransactionDetail.vue'
import TransactionEdit from '@/views/TransactionEdit.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/properties',
    name: 'PropertyList',
    component: PropertyList
  },
  {
    path: '/properties/new',
    name: 'PropertyNew',
    component: PropertyEdit
  },
  {
    path: '/properties/:id',
    name: 'PropertyDetail',
    component: PropertyDetail,
    props: true
  },
  {
    path: '/properties/:id/edit',
    name: 'PropertyEdit',
    component: PropertyEdit,
    props: true
  },
  {
    path: '/clients',
    name: 'ClientList',
    component: ClientList
  },
  {
    path: '/clients/new',
    name: 'ClientNew',
    component: ClientEdit
  },
  {
    path: '/clients/:id/edit',
    name: 'ClientEdit',
    component: ClientEdit,
    props: true
  },
  {
    path: '/contracts',
    name: 'ContractList',
    component: ContractList
  },
  {
    path: '/contracts/new',
    name: 'ContractNew',
    component: ContractEdit
  },
  {
    path: '/contracts/:id',
    name: 'ContractDetail',
    component: ContractDetail,
    props: true
  },
  {
    path: '/contracts/:id/edit',
    name: 'ContractEdit',
    component: ContractEdit,
    props: true
  },
  {
    path: '/transactions',
    name: 'TransactionList',
    component: TransactionList
  },
  {
    path: '/transactions/new',
    name: 'TransactionNew',
    component: TransactionEdit
  },
  {
    path: '/transactions/:id',
    name: 'TransactionDetail',
    component: TransactionDetail,
    props: true
  },
  {
    path: '/transactions/:id/edit',
    name: 'TransactionEdit',
    component: TransactionEdit,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
