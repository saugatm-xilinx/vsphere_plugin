import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';
import { NicService } from '../../services';

@Component({
  selector: 'app-nic',
  templateUrl: './nic.component.html',
  styleUrls: ['./nic.component.scss']
})
export class NicComponent implements OnInit {

  private routeChangeSubscription: Subscription;

  constructor(private router: Router, private activatedRoute: ActivatedRoute,
    private nicSvc: NicService) { }

  ngOnInit() { }

  redirect(route) {
    this.router.navigate([route], { relativeTo: this.activatedRoute })
  }

}
