import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  nickname: string = '';

  constructor(private router: Router) {}

  onLogin() {
    if (this.nickname) {
      this.router.navigate([`/chat/${this.nickname}`]);
    }
  }
}
