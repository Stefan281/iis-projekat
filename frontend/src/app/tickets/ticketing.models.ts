export type MatchStatus = 'SCHEDULED' | 'CANCELLED' | 'FINISHED';
export type MatchAttractiveness = 'LOW' | 'MEDIUM' | 'HIGH' | 'DERBY';
export type SeatStatus = 'AVAILABLE' | 'RESERVED' | 'SOLD' | 'BLOCKED';
export type PromotionStatus = 'ACTIVE' | 'INACTIVE' | 'EXPIRED';
export type TicketStatus = 'VALID' | 'CANCELLED' | 'REFUNDED';
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
  startDate: string;
  endDate: string;
  status: PromotionStatus;
}

export type PromotionRequest = Omit<Promotion, 'id'>;

export interface TicketType {
  id: number;
  name: string;
  description: string;
  coefficient: number;
}

export type TicketTypeRequest = Omit<TicketType, 'id'>;

export interface PurchaseRequest {
  matchId: number;
  seatId: number;
}

export interface ReservationRequest {
  matchId: number;
  seatId: number;
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
