import { Component, OnInit, OnDestroy } from '@angular/core';
import { WebSocketService } from './core/services/websocket.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Reagent Dispenser';
  wsConnected = false;

  constructor(private webSocketService: WebSocketService) {}

  ngOnInit(): void {
    this.webSocketService.connect();
    
    this.webSocketService.isConnected().subscribe(
      connected => {
        this.wsConnected = connected;
        console.log('WebSocket connection status:', connected);
      }
    );
  }

  ngOnDestroy(): void {
    this.webSocketService.disconnect();
  }
}
