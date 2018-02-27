import { Component, OnInit, OnDestroy } from '@angular/core';
import { environment } from 'environments/environment';
import { Params, ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';
import { Nic } from '../nic.model';
import { NicService } from '../../../services';

@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit, OnDestroy {

  isProd = environment.production;
  nicDetail = new Nic();
  hasError = false;
  isLoading = true;
  isFirstTimeLoading = true;
  routeChangeSubscription: Subscription;

  constructor(private activatedRoute: ActivatedRoute, private route: Router, private nicSvc: NicService) { }

  ngOnInit() {

    this.routeChangeSubscription = this.activatedRoute.parent.paramMap.subscribe(params => {
      this.isLoading = true;
      const hostId = params.get('hostId');
      const nicId = params.get('nicId');
      this.nicSvc.setUrlParts(hostId, nicId);
      this.getDataFromStorage(hostId, nicId);
      this.getNicDetail();
    });

  }

  ngOnDestroy() {
    this.routeChangeSubscription.unsubscribe();
  }

  getDataFromStorage(hostId, nicId) {
    if (this.isFirstTimeLoading) {
      this.nicDetail = this.nicSvc.getNicFromStorage(hostId, nicId);
      if (this.nicDetail) {
        this.hasError = false;
        this.isLoading = false;
      }
      this.isFirstTimeLoading = false;
    }
  }

  refrechNicDetail() {
    this.isLoading = true;
    this.getNicDetail();
  }

  getNicDetail() {
    this.nicSvc.getNicDetails()
      .subscribe(
        data => {
          this.isLoading = false;
          const nic = this.nicSvc.buildNicByAdapter(data, null);
          if (nic && nic.length === 1) {
            this.nicDetail = nic[0];
          } else {
            this.nicDetail = new Nic();
          }
        },
        err => {
          this.isLoading = false;
          this.hasError = true;
        }
      );
  }
}
