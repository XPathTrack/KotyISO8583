package org.refactor.ui.layout_managers.percent_layout;

import org.refactor.utils.ToolBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PercentLayout implements LayoutManager2 {

    private final Dimension referenceDimension;
    private Rectangle parentBounds;
    private int percentParent;
    private final HashMap<Component, PercentData> percentMap = new HashMap<>();
    private final ArrayList<Component> purgeList = new ArrayList<>();

    public PercentLayout(Dimension referenceDimension) {
        this.referenceDimension = referenceDimension;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        synchronized (comp.getTreeLock()) {
            purgeList.add(comp);
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            System.out.println("preferredLayoutSize");
            return referenceDimension;
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            System.out.println("minimumLayoutSize");
            return referenceDimension;
        }
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            System.out.println("layoutContainer");
            syncPurge(parent);

            Rectangle parentBounds = parent.getBounds();
            percentParent = (parentBounds.height * 100) / referenceDimension.height;
            if (this.parentBounds == null || this.parentBounds.height != parentBounds.height) {
                this.parentBounds = parentBounds;
                printAll();
            } else {
                syncAll();
            }
        }
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        synchronized (comp.getTreeLock()) {
            if (!(constraints instanceof PercentData.Builder))
                throw new IllegalArgumentException("The constraint parameter must be " + PercentData.class);

            PercentData percentData = ((PercentData.Builder) constraints).build();
            if (percentData == null)
                throw new NullPointerException("All percentage attributes are null.");

            System.out.println("addLayoutComponent = " + " time: " + System.currentTimeMillis() + comp);
            percentMap.put(comp, percentData);
        }
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            System.out.println("maximumLayoutSize");
            return referenceDimension;
        }
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        synchronized (target.getTreeLock()) {
            System.out.println("getLayoutAlignmentX");
            return 0;
        }
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        synchronized (target.getTreeLock()) {
            System.out.println("getLayoutAlignmentY");
            return 0;
        }
    }

    @Override
    public void invalidateLayout(Container target) {
        synchronized (target.getTreeLock()) {
            System.out.println("invalidateLayout");
        }
    }

    private void syncPurge(Container parent) {
        if (purgeList.isEmpty())
            return;

        for (Component component : purgeList) {
            parent.remove(component);
            percentMap.remove(component);
        }

        purgeList.clear();
    }

    private void syncComponent(Component component, PercentData percentData) {
        if (percentData.percentBounds != null)
            setBounds(component, percentData.percentBounds);

        if (percentData.font != null)
            setFont(component, percentData.font);

        percentData.painted = true;

        System.out.println("sync Component");
    }

    private void printAll() {
        for (Map.Entry<Component, PercentData> componentData : percentMap.entrySet()) {
            Component component = componentData.getKey();
            PercentData percentData = componentData.getValue();
            syncComponent(component, percentData);
        }
    }

    private void syncAll() {
        for (Map.Entry<Component, PercentData> componentData : percentMap.entrySet()) {
            Component component = componentData.getKey();
            PercentData percentData = componentData.getValue();
            if (percentData.painted)
                continue;

            syncComponent(component, percentData);
        }
    }

    public void setBounds(Component component, Rectangle percentBounds) {
        int width = ToolBox.percent(parentBounds.width, percentBounds.width);
        int height = ToolBox.percent(parentBounds.height, percentBounds.height);
        int x = ToolBox.percent(parentBounds.width, percentBounds.x);
        int y = ToolBox.percent(parentBounds.height, percentBounds.y);
        component.setBounds(x - width / 2, y - height / 2, width, height);
    }

    public void setFont(Component component, Font baseFont) {
        float percentSize = ToolBox.percent(baseFont.getSize(), ToolBox.percent(percentParent, 100));
        Font newF = baseFont.deriveFont(percentSize);
        component.setFont(newF);
    }
}