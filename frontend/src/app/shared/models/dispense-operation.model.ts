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

export interface CreateBatchRequest {
  plateBarcode: string;
}

export interface AddOperationToBatchRequest {
  wellPosition: string;
  reagentId: number;
  volume: number;
}

export interface DispenseBatch {
  id: number;
  plateId: number;
  plateBarcode: string;
  status: BatchStatus;
  createdDate: string;
  executionStartedDate?: string;
  completedDate?: string;
  operationCount: number;
  operations?: DispenseOperation[];
}

export enum OperationStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

export enum BatchStatus {
  PLANNED = 'PLANNED',
  EXECUTING = 'EXECUTING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}
