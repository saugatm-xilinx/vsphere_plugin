import { Component, OnInit, OnDestroy } from '@angular/core';
import { environment } from 'environments/environment';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute } from '@angular/router';
import { NicService } from '../../../services';

@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss']
})
export class StatisticsComponent implements OnInit, OnDestroy {

  isProd = environment.production;
  hasError = false;
  isLoading = false;
  routeChangeSubscription: Subscription;
  statisticTableData: any;

  constructor(private activatedRoute: ActivatedRoute, private nicSvc: NicService) { }

  ngOnInit() {

    this.routeChangeSubscription = this.activatedRoute.parent.paramMap.subscribe(params => {
      this.nicSvc.setUrlParts(params.get('hostId'), params.get('nicId'));
      this.getStatDetail();
    });

  }

  ngOnDestroy() {
    this.routeChangeSubscription.unsubscribe();
  }

  getStatDetail() {
    this.isLoading = true;
    this.hasError = false;
    this.nicSvc.getStatDetails()
      .subscribe(
        data => {
          this.statisticTableData = data;
          this.isLoading = false;
          this.hasError = false;
        },
        err => {
          if(err.status == 401){
            // window.location.reload()
          }
          this.isLoading = false;
          this.hasError = true;
        }
      );
  }
}
