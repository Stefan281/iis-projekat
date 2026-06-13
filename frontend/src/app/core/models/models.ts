export type TripStatus = 'IN_PROCESSING' | 'AWAITING_APPROVAL' | 'CONFIRMED' | 'REJECTED' | 'COMPLETED';
export type DocumentationStatus = 'TO_CHECK' | 'COMPLETE' | 'MISSING';
export type TransportType = 'AUTOBUS' | 'KOMBI' | 'AVION' | 'VOZ';

export interface Trip {
  id: number;
  name: string;
  location: string;
  purpose: string;
  departureDate: string;   // ISO "2026-05-19"
  returnDate?: string;     // null for single-day trips
  status: TripStatus;
  smestajId?: number;
  transportId?: number;
  organizatorId?: number;
  razlogOdbijanja?: string;
  selectedSmestaj?: AccommodationOffer | null;
  selectedTransport?: TransportOffer | null;
}

export interface AccommodationOffer {
  id: number;
  putovanjeId: number;
  ime: string;
  adresa: string;
  cena: number;
  izabran: boolean;
}

export interface TransportOffer {
  id: number;
  putovanjeId: number;
  naziv: string;
  vrsta: TransportType;
  cena: number;
  izabran: boolean;
}

export interface Passenger {
  id: number;                  // userId
  userId?: number;             // backend duplicates id for clarity; not always present
  participantId?: number;      // present only when added=true
  ime: string;
  prezime: string;
  role?: string;
  sobaBroj?: string;
  dokumentacijaStatus?: DocumentationStatus;
  added: boolean;              // true if added to this trip
}

export interface Announcement {
  id: number;
  tekst: string;
  datum: string;
  autorId: number;
}

export interface Message {
  id: number;
  posiljacId: number;
  posiljacIme: string;
  tekst: string;
  datum: string;
  procitana: boolean;
}
