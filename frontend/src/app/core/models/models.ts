export type PutovanjeStatus = 'IN_PROCESSING' | 'AWAITING_APPROVAL' | 'CONFIRMED' | 'REJECTED' | 'COMPLETED';
export type DokumentacijaStatus = 'KOMPLETNO' | 'PROVERITI' | 'NEDOSTAJE';
export type VrstaPrevoza = 'AUTOBUS' | 'KOMBI' | 'AVION' | 'VOZ';

export interface Putovanje {
  id: number;
  name: string;
  location: string;
  purpose: string;
  departureDate: string;   // ISO "2026-05-19"
  returnDate?: string;     // null for single-day trips
  status: PutovanjeStatus;
  smestajId?: number;
  transportId?: number;
  organizatorId?: number;
  razlogOdbijanja?: string;
}

export interface PonudaSmestaja {
  id: number;
  putovanjeId: number;
  ime: string;
  adresa: string;
  cena: number;
  izabran: boolean;
}

export interface PonudaTransporta {
  id: number;
  putovanjeId: number;
  naziv: string;
  vrsta: VrstaPrevoza;
  cena: number;
  izabran: boolean;
}

export interface Putnik {
  id: number;
  ime: string;
  prezime: string;
  sobaBroj?: string;
  dokumentacijaStatus: DokumentacijaStatus;
  dodat: boolean; // true if added to this trip
}

export interface Obavestenje {
  id: number;
  tekst: string;
  datum: string;
  autorId: number;
}

export interface Poruka {
  id: number;
  posiljacId: number;
  posiljacIme: string;
  tekst: string;
  datum: string;
  procitana: boolean;
}
