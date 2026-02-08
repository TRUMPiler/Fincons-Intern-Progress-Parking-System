import { Component, OnInit } from '@angular/core';
import { Form, FormsModule, NgForm } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ChangeDetectorRef } from '@angular/core';
import { RouterLink } from '@angular/router';
import {ToastModule} from 'primeng/toast';
import {RippleModule} from 'primeng/ripple';


@Component({
  selector: 'app-parkinglot-registeration',
  imports: [FormsModule, CommonModule, TableModule, ButtonModule,DialogModule,RouterLink],
  standalone: true,
  templateUrl: 'homepage.html',
  styleUrl: './homepage.css',
})
export class Homepage  {}