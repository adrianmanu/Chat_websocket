import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChatComponent } from './components/chat/chat.component';
import { LoginComponent } from './login/login.component'; // Importa el componente de login

const routes: Routes = [
  { path: '', component: LoginComponent }, // Ruta para el login en la raíz
  { path: 'chat/:userId', component: ChatComponent } // Ruta para el chat
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
