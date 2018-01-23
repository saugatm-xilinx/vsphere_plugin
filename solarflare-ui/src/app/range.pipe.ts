import { Pipe, PipeTransform } from '@angular/core';

//TODO: review comments - create pipes directiory in shared module and move there.  
@Pipe({
    name: 'range',
    pure: false
})

export class RangePipe implements PipeTransform {
    transform(items: any[], quantity: number): any {
        items.length = 0;
        for (let i = 0; i < quantity; i++) {
            items.push(i);
        }
        return items;
    }
}