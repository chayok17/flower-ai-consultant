from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter


DRAWABLE_DIR = Path(
    r"C:\Users\irlan\AndroidStudioProjects\FlowerAIConsultant\app\src\main\res\drawable"
)
SOURCE = Path(r"C:\all\mine\photos\букет 1 версия.png")
CROPS = {
    "bouquet5_black.png": (0, 0, 512, 1024),
    "bouquet5_pink.png": (512, 0, 1024, 1024),
    "bouquet5_white.png": (1024, 0, 1536, 1024),
}


def soft_cutout(image: Image.Image, file_name: str) -> Image.Image:
    image = image.convert("RGBA")
    width, height = image.size
    mask = Image.new("L", (width, height), 0)
    draw = ImageDraw.Draw(mask)

    if file_name == "bouquet5_black.png":
        draw.ellipse((25, 185, 455, 650), fill=255)
        draw.polygon([(45, 330), (450, 250), (455, 690), (330, 850), (150, 830), (60, 610)], fill=255)
        draw.rectangle((105, 675, 380, 900), fill=255)
    elif file_name == "bouquet5_white.png":
        draw.ellipse((12, 225, 505, 655), fill=255)
        draw.polygon([(10, 340), (500, 270), (500, 690), (360, 830), (180, 790), (35, 660)], fill=255)
        draw.rectangle((165, 650, 360, 860), fill=255)
    else:
        draw.ellipse((0, 215, 512, 665), fill=255)
        draw.polygon([(10, 330), (505, 250), (512, 690), (330, 890), (155, 840), (25, 650)], fill=255)
        draw.rectangle((170, 640, 365, 920), fill=255)

    mask = mask.filter(ImageFilter.GaussianBlur(10))
    image.putalpha(mask)
    bbox = mask.point(lambda value: 255 if value > 20 else 0).getbbox()
    if bbox:
        image = image.crop(bbox)
    return image


def main() -> None:
    source = Image.open(SOURCE).convert("RGB")
    for file_name, crop_box in CROPS.items():
        path = DRAWABLE_DIR / file_name
        cutout = soft_cutout(source.crop(crop_box), file_name)
        cutout.save(path, optimize=True)
        print(f"saved {path}")


if __name__ == "__main__":
    main()
