export interface WebSocketMessage<T = any> {
  type: string;
  payload: T;
  timestamp: string;
}
