export type MatchStatus = 'SCHEDULED' | 'CANCELLED' | 'FINISHED';
export type MatchAttractiveness = 'LOW' | 'MEDIUM' | 'HIGH' | 'DERBY';
export type SeatStatus = 'AVAILABLE' | 'RESERVED' | 'SOLD' | 'BLOCKED';
export type PromotionStatus = 'ACTIVE' | 'INACTIVE' | 'EXPIRED';
export type PromotionType = 'PERCENTAGE';
export type TicketStatus = 'VALID' | 'CANCELLED';
export type ReservationStatus = 'ACTIVE' | 'CANCELLED' | 'EXPIRED' | 'SOLD';

export interface Match {
  id: number;
  date: string;
  time: string;
  homeTeam: string;
  awayTeam: string;
  location: string;
  status: MatchStatus;
  basePrice: number;
  attractiveness: MatchAttractiveness;
  expectedAttendance: number;
}

export type MatchRequest = Omit<Match, 'id'>;

export interface Zone {
  id: number;
  name: string;
  description: string;
  priceCoefficient: number;
  capacity: number;
  occupiedSeats: number;
  occupancyRate: number;
}

export interface ZoneRequest {
  name: string;
  description: string;
  priceCoefficient: number;
  capacity: number;
}

export interface Seat {
  id: number;
  rowLabel: string;
  seatNumber: number;
  status: SeatStatus;
  zoneId: number;
  zoneName: string;
}

export interface SeatRequest {
  rowLabel: string;
  seatNumber: number;
  status: SeatStatus;
  zoneId: number;
}

export interface Promotion {
  id: number;
  name: string;
  discountPercentage: number;
  status: PromotionStatus;
  minTickets: number;
  promotionType: PromotionType;
  promoCode: string | null;
}

export interface PromotionRequest {
  name: string;
  discountPercentage: number;
  active: boolean;
  minTickets: number;
  promotionType: PromotionType;
  promoCode: string | null;
}

export interface TicketType {
  id: number;
  name: string;
  description: string;
  coefficient: number;
}

export type TicketTypeRequest = Omit<TicketType, 'id'>;

export interface PurchaseRequest {
  matchId: number;
  seatId?: number;
  seatIds?: number[];
  promotionId?: number;
}

export interface ReservationRequest {
  matchId: number;
  seatId: number;
  promotionId?: number;
}

export interface Ticket {
  id: number;
  matchId: number;
  homeTeam: string;
  awayTeam: string;
  matchDate: string;
  matchTime: string;
  location: string;
  seatId: number;
  rowLabel: string;
  seatNumber: number;
  zoneId: number;
  zoneName: string;
  price: number;
  status: TicketStatus;
  purchasedAt: string;
}

export type PricingRuleCondition =
  | 'DERBY' | 'HIGH_ATTRACTIVENESS' | 'LOW_ATTRACTIVENESS'
  | 'HIGH_OCCUPANCY' | 'MEDIUM_OCCUPANCY' | 'LOW_OCCUPANCY'
  | 'WEEKEND' | 'TODAY' | 'EARLY_BIRD';

export interface PricingRule {
  id: number;
  name: string;
  description: string;
  condition: PricingRuleCondition;
  coefficient: number;
  active: boolean;
  priority: number;
}

export type PricingRuleRequest = Omit<PricingRule, 'id'>;

export interface AppliedRule {
  name: string;
  coefficient: number;
}

export interface PriceTotalResponse {
  baseTotal: number;
  discount: number;
  finalTotal: number;
  promotionName: string | null;
}

export interface PriceBreakdown {
  basePrice: number;
  zoneCoefficient: number;
  zoneName: string;
  ticketTypeCoefficient: number;
  ticketTypeName: string;
  appliedRules: AppliedRule[];
  promotionDiscount: number;
  promotionName: string;
  finalPrice: number;
}

export interface PriceHistoryEntry {
  id: number;
  pricingRuleId: number;
  pricingRuleName: string;
  oldCoefficient: number;
  newCoefficient: number;
  description: string;
  source: string;
  changedAt: string;
}

export interface MatchSalesStats {
  matchId: number;
  homeTeam: string;
  awayTeam: string;
  matchDate: string;
  soldTickets: number;
  activeReservations: number;
  availableSeats: number;
  revenue: number;
}

export interface Reservation {
  id: number;
  customerId: number;
  customerFullName: string;
  matchId: number;
  homeTeam: string;
  awayTeam: string;
  matchDate: string;
  matchTime: string;
  location: string;
  seatId: number;
  rowLabel: string;
  seatNumber: number;
  zoneId: number;
  zoneName: string;
  price: number;
  status: ReservationStatus;
  createdAt: string;
  expiresAt: string;
}
