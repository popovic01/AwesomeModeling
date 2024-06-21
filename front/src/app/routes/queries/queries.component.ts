import { Component } from '@angular/core';
import { BackendService, QOne } from '../../backend.service';

@Component({
  selector: 'app-queries',
  standalone: true,
  imports: [],
  templateUrl: './queries.component.html',
  styleUrl: './queries.component.css'
})
export class QueriesComponent {
  public qones: QOne[] | undefined;

  constructor(private backendService: BackendService) {
    this.backendService.getQOnes().subscribe((res) => {
      this.qones = res;
      console.log(this.qones);
    })
  }

}
