import { Component, OnInit, OnDestroy } from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';


@Component({
  selector: 'app-host',
  templateUrl: './host.component.html',
  styleUrls: ['./host.component.scss']
})
export class HostComponent implements OnInit, OnDestroy {


  constructor(private router: Router,
              private activatedRoute: ActivatedRoute) { }

  ngOnInit() {


  }



    ngOnDestroy(){
    }


    redirect(route) {
        this.router.navigate([route], {relativeTo: this.activatedRoute})
    }
}
