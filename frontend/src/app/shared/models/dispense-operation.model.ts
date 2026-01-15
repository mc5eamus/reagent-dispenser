export interface DispenseOperation {
  id?: number;
  plateId: number;
  plateBarcode?: string;
  wellId: number;
  wellPosition?: string;
  reagentId: number;
  reagentName?: string;
  volumeDispensed: number;
  status: OperationStatus;
  createdDate?: string;
  completedDate?: string;
  errorMessage?: string;
}

export interface DispenseRequest {
  plateBarcode: string;
  wellPosition: string;
  reagentId: number;
  volume: number;
}

export enum OperationStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}
