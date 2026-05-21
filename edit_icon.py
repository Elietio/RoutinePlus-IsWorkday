import os
from PIL import Image, ImageDraw

def apply_circular_mask(img_path):
    try:
        img = Image.open(img_path).convert("RGBA")
        w, h = img.size
        
        # Create a high-resolution mask for anti-aliasing
        mask_scale = 4
        mask = Image.new("L", (w * mask_scale, h * mask_scale), 0)
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0, w * mask_scale, h * mask_scale), fill=255)
        
        # Resize mask down with antialiasing
        mask = mask.resize((w, h), Image.Resampling.LANCZOS)
        
        # Apply mask
        img.putalpha(mask)
        
        img.save(img_path, "PNG")
        print(f"Processed {img_path}")
    except Exception as e:
        print(f"Error processing {img_path}: {e}")

res_dir = "app/src/main/res"
for folder in os.listdir(res_dir):
    if folder.startswith("mipmap-"):
        folder_path = os.path.join(res_dir, folder)
        for file in os.listdir(folder_path):
            if file.endswith(".png"):
                file_path = os.path.join(folder_path, file)
                apply_circular_mask(file_path)

print("Done processing all icons.")
