package net.yangziwen.hqlformatter;

import java.util.List;

import net.yangziwen.hqlformatter.util.TableCache;
import net.yangziwen.hqlformatter.util.TableCache.RelationGraph;
import net.yangziwen.hqlformatter.util.TableCache.TableWrapper;

public class Test {
	
	public static void main(String[] args) throws Exception {
		
		RelationGraph graph = TableCache.getTableRelationGraph(152L, 3);
		
		System.out.println(graph.getTable());
		
		int idx = 0;
		for (List<TableWrapper> list : graph.getDependentLayers()) {
			System.out.println("---------layer " + idx + "------------");
			for (TableWrapper tbl : list) {
				System.out.println(tbl);
			}
			idx ++;
		}
	}
	
}
