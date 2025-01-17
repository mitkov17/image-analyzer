import { Component } from '@angular/core';
import { ImageService } from '../../services/image.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  selectedFile: File | null = null;
  uploadMessage: string = '';
  keyword: string = '';
  images: string[] = [];
  searched = false;
  messageClass: string = '';

  constructor(private imageService: ImageService) {}

  ngOnInit(): void {
    this.search();
  }

  onFileSelected(event: any): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      this.selectedFile = input?.files[0];
    }

    input.value = '';
  }

  upload(): void {
    if (this.selectedFile) {
      this.imageService.uploadImage(this.selectedFile).subscribe({
        next: () => {
          this.uploadMessage = 'Upload successful!';
          this.messageClass = 'success';

          setTimeout(() => {
            this.uploadMessage = '';
            this.selectedFile = null;
          }, 10000);
        },
        error: (err) => {
          this.uploadMessage = 'Upload failed. Please try again.';
          this.messageClass = 'error';
          console.error(err);
        }
      });
    }
  }

  search(): void {
    const searchKeyword = this.keyword.trim();

    this.imageService.searchImages(searchKeyword || '').subscribe({
      next: (data: string[]) => {
        this.images = data;
        this.searched = true;
      },
      error: (err) => {
        console.error(err);
        this.images = [];
        this.searched = true;
      }
    });
  }
}
