import { Injectable } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { WebSocketMessage } from '../../shared/models/websocket-message.model';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private connectionState$ = new BehaviorSubject<boolean>(false);
  private messageSubject$ = new BehaviorSubject<WebSocketMessage | null>(null);

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl) as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (msg: string) => {
        console.log('STOMP:', msg);
      }
    });

    this.client.onConnect = () => {
      console.log('WebSocket connected');
      this.connectionState$.next(true);
    };

    this.client.onDisconnect = () => {
      console.log('WebSocket disconnected');
      this.connectionState$.next(false);
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP error:', frame);
      this.connectionState$.next(false);
    };
  }

  connect(): void {
    if (!this.client.active) {
      this.client.activate();
    }
  }

  disconnect(): void {
    if (this.client.active) {
      this.client.deactivate();
    }
  }

  isConnected(): Observable<boolean> {
    return this.connectionState$.asObservable();
  }

  subscribe<T>(destination: string): Observable<WebSocketMessage<T>> {
    return new Observable(observer => {
      let subscription: StompSubscription | null = null;

      const subscribe = () => {
        if (this.client.connected) {
          subscription = this.client.subscribe(destination, (message: IMessage) => {
            try {
              const parsedMessage = JSON.parse(message.body) as WebSocketMessage<T>;
              observer.next(parsedMessage);
            } catch (error) {
              console.error('Error parsing WebSocket message:', error);
              observer.error(error);
            }
          });
        }
      };

      // Subscribe immediately if connected, or wait for connection
      if (this.client.connected) {
        subscribe();
      } else {
        const connectionSub = this.connectionState$
          .pipe(filter(connected => connected))
          .subscribe(() => {
            subscribe();
          });

        return () => {
          connectionSub.unsubscribe();
          if (subscription) {
            subscription.unsubscribe();
          }
        };
      }

      return () => {
        if (subscription) {
          subscription.unsubscribe();
        }
      };
    });
  }

  send(destination: string, body: any): void {
    if (this.client.connected) {
      this.client.publish({
        destination,
        body: JSON.stringify(body)
      });
    } else {
      console.error('Cannot send message: WebSocket not connected');
    }
  }
}
