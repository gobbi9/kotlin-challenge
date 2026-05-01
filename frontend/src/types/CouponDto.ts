export interface CouponDto {
  id?: string
  code: string
  discount: number
  description: string
  applicationCount?: number
  version?: number
  creationDateTime?: string
  updateDateTime?: string
}

export interface CouponListResponse {
  coupons: CouponDto[]
  totalCount: number
  page: number
  pageSize: number
}
