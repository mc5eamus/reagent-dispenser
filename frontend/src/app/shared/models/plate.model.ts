export interface Plate {
  id?: number;
  barcode: string;
  rows: number;
  columns: number;
  plateType: string;
  createdDate?: string;
  wells?: Well[];
}

export interface Well {
  id?: number;
  position: string;
  plateId?: number;
  volume?: number;
  maxVolume: number;
}
