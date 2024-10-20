package org.refactor.ui.layout_managers.percent_layout;

import java.awt.*;

public class PercentData {
    protected Rectangle percentBounds;
    protected Font font;
    protected boolean painted;

    protected PercentData() {
    }

    protected PercentData(PercentData percentData) {
        this.percentBounds = percentData.percentBounds;
        this.font = percentData.font;
        this.painted = percentData.painted;
    }

    public static class Builder {

        private final PercentData percentData = new PercentData();

        public Builder() {
        }

        public Builder(Component component) {
            percentData.percentBounds = component.getBounds();
            percentData.font = component.getFont();
        }

        public Rectangle getPercentBounds() {
            return percentData.percentBounds;
        }

        public void setPercentBounds(Rectangle percentBounds) {
            percentData.percentBounds = percentBounds;
        }

        public Font getFont() {
            return percentData.font;
        }

        public void setFont(Font font) {
            percentData.font = font;
        }

        protected PercentData build() {
            boolean hasBounds = percentData.percentBounds != null;
            boolean hasFont = percentData.font != null;

            if (!hasBounds && !hasFont)
                return null;

            if (hasBounds) {
                percentData.percentBounds = new Rectangle(percentData.percentBounds);
            }

            if (hasFont) {
                percentData.font = percentData.font.deriveFont(percentData.font.getStyle());
            }

            return new PercentData(percentData);
        }
    }
}
