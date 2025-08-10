export interface Property {
  id?: number
  name: string
  address: string
  description: string
  type: PropertyType
  status: PropertyStatus
  price: number
  area: number
  rooms: number
  bathrooms: number
  parkingSpaces: number
  yearBuilt: number
  createdAt?: string
  updatedAt?: string
}

export enum PropertyType {
  APARTMENT = 'APARTMENT',
  HOUSE = 'HOUSE',
  COMMERCIAL = 'COMMERCIAL',
  LAND = 'LAND',
  OFFICE = 'OFFICE',
  WAREHOUSE = 'WAREHOUSE'
}

export enum PropertyStatus {
  AVAILABLE = 'AVAILABLE',
  SOLD = 'SOLD',
  RENTED = 'RENTED',
  UNDER_CONTRACT = 'UNDER_CONTRACT',
  MAINTENANCE = 'MAINTENANCE'
}

export interface Client {
  id?: number
  firstName: string
  lastName: string
  email: string
  phone: string
  address: string
  type: ClientType
  createdAt?: string
  updatedAt?: string
}

export enum ClientType {
  BUYER = 'BUYER',
  SELLER = 'SELLER',
  TENANT = 'TENANT',
  LANDLORD = 'LANDLORD'
}

export interface Contract {
  id?: number
  contractNumber: string
  propertyId: number
  clientId: number
  propertyName: string
  clientName: string
  type: ContractType
  status: ContractStatus
  amount: number
  monthlyRent?: number
  startDate: string
  endDate: string
  terms: string
  createdAt?: string
  updatedAt?: string
}

export enum ContractType {
  SALE = 'SALE',
  RENTAL = 'RENTAL',
  LEASE = 'LEASE',
  MANAGEMENT = 'MANAGEMENT'
}

export enum ContractStatus {
  DRAFT = 'DRAFT',
  ACTIVE = 'ACTIVE',
  EXPIRED = 'EXPIRED',
  TERMINATED = 'TERMINATED',
  PENDING = 'PENDING'
}

export interface Transaction {
  id?: number
  contractId: number
  type: TransactionType
  amount: number
  monthlyRent?: number
  transactionDate: string
  description: string
  status: TransactionStatus
  createdAt?: string
  updatedAt?: string
}

export enum TransactionType {
  PAYMENT = 'PAYMENT',
  REFUND = 'REFUND',
  COMMISSION = 'COMMISSION',
  MAINTENANCE = 'MAINTENANCE',
  INSURANCE = 'INSURANCE',
  TAX = 'TAX'
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}
