export interface PlannedOperation {
  id: string; // Temporary ID for tracking in the plan (e.g., UUID)
  wellPosition: string;
  wellId?: number;
  reagentId: number;
  reagentName?: string;
  volume: number;
  status: PlannedOperationStatus;
  error?: string;
}

export enum PlannedOperationStatus {
  PLANNED = 'PLANNED',
  EXECUTING = 'EXECUTING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}
