import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';

@Component({
    selector: 'app-host',
    templateUrl: './host.component.html',
    styleUrls: ['./host.component.scss']
})
export class HostComponent implements OnInit {

    constructor(private router: Router, private activatedRoute: ActivatedRoute) { }

    ngOnInit() {
        this.router.navigate(['overview'], { relativeTo: this.activatedRoute })
    }

    redirect(route) {
        this.router.navigate([route], { relativeTo: this.activatedRoute })
    }
}
