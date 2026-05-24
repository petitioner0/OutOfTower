package outoftower.debug;

import outoftower.map.nodes.AbstractMapNode;
import outoftower.util.NodeRegistry;

public class NodeMatrixPrinter {
    public static void main(String[] args) {

        int size = 11;
        char[][] grid = new char[size][size];

        // 初始化为空 '-'
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                grid[y][x] = '-';
            }
        }

        // 如果你之后有节点，它们会在这里填进去
        for (AbstractMapNode node : NodeRegistry.getAllNodes()) {
            if (node.gx >= 0 && node.gx < size && node.gy >= 0 && node.gy < size) {
                grid[node.gy][node.gx] = '+';
            }
        }

        // 打印
        System.out.println("===== NODE MATRIX =====");
        for (int y = size - 1; y >= 0; y--) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < size; x++) {
                sb.append(grid[y][x]).append(' ');
            }
            System.out.println(sb);
        }
        System.out.println("=======================");
    }
}
