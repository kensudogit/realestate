import axios from 'axios'
import type { Property, Client, Contract, Transaction } from '@/types'

const API_BASE_URL = 'http://localhost:8081/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Property API
export const propertyApi = {
  getAll: () => api.get<Property[]>('/properties'),
  getProperties: (params?: any) => api.get<Property[]>('/properties', { params }),
  getById: (id: number) => api.get<Property>(`/properties/${id}`),
  create: (property: Property) => api.post<Property>('/properties', property),
  update: (id: number, property: Property) => api.put<Property>(`/properties/${id}`, property),
  delete: (id: number) => api.delete(`/properties/${id}`),
  search: (query: string) => api.get<Property[]>(`/properties/search?query=${query}`),
  getByType: (type: string) => api.get<Property[]>(`/properties/type/${type}`),
  getByStatus: (status: string) => api.get<Property[]>(`/properties/status/${status}`),
  searchByCriteria: (params: any) => api.get<Property[]>('/properties/search/criteria', { params })
}

// Client API
export const clientApi = {
  getAll: () => api.get<Client[]>('/clients'),
  getClients: (params?: any) => api.get<Client[]>('/clients', { params }),
  getById: (id: number) => api.get<Client>(`/clients/${id}`),
  create: (client: Client) => api.post<Client>('/clients', client),
  update: (id: number, client: Client) => api.put<Client>(`/clients/${id}`, client),
  delete: (id: number) => api.delete(`/clients/${id}`),
  getByType: (type: string) => api.get<Client[]>(`/clients/type/${type}`)
}

// Contract API
export const contractApi = {
  getAll: () => api.get<Contract[]>('/contracts'),
  getContracts: (params?: any) => api.get<Contract[]>('/contracts', { params }),
  getById: (id: number) => api.get<Contract>(`/contracts/${id}`),
  create: (contract: Contract) => api.post<Contract>('/contracts', contract),
  update: (id: number, contract: Contract) => api.put<Contract>(`/contracts/${id}`, contract),
  delete: (id: number) => api.delete(`/contracts/${id}`),
  getByType: (type: string) => api.get<Contract[]>(`/contracts/type/${type}`),
  getByStatus: (status: string) => api.get<Contract[]>(`/contracts/status/${status}`)
}

// Transaction API
export const transactionApi = {
  getAll: () => api.get<Transaction[]>('/transactions'),
  getTransactions: (params?: any) => api.get<Transaction[]>('/transactions', { params }),
  getById: (id: number) => api.get<Transaction>(`/transactions/${id}`),
  create: (transaction: Transaction) => api.post<Transaction>('/transactions', transaction),
  update: (id: number, transaction: Transaction) => api.put<Transaction>(`/transactions/${id}`, transaction),
  delete: (id: number) => api.delete(`/transactions/${id}`)
}

export default api
