import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';

@Component({
    selector: 'app-adapter',
    templateUrl: './adapter.component.html',
    styleUrls: ['./adapter.component.scss']
})
export class AdapterComponent implements OnInit {

    constructor(private router: Router,
        private activatedRoute: ActivatedRoute) { }

    ngOnInit() { }

    redirect(route) {
        this.router.navigate([route], { relativeTo: this.activatedRoute })
    }
}
