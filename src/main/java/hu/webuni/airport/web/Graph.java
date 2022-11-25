package hu.webuni.airport.web;

import java.util.Arrays;


public class Graph
{
    int[][] adjMatrix;
    int numOfvertices;

    Graph(int[][] mat, int v)
    {
        this.adjMatrix = mat;
        this.numOfvertices = v;
    }

    void addEdge(int src, int dest, int edgeWeight)
    {
        adjMatrix[src][dest] = edgeWeight;
        adjMatrix[dest][src] = edgeWeight;
    }

    public static int getClosestVertex(int[] distance, boolean[] visited)
    {
        int min = Integer.MAX_VALUE;
        int minIdx = -1;
        for(int i=0; i<distance.length; i++)
        {
            if(distance[i] < min)
                if(visited[i] == false)
                {
                    min = distance[i];
                    minIdx = i;
                }
        }
        return minIdx;
    }

    public static int[] dijkstrasShortestPath(Graph g, int src)
    {
        //final shortest distance array
        int[] distance = new int[g.numOfvertices];
        //array to tell whether shortest distance of vertex has been found
        boolean[] visited = new boolean[g.numOfvertices];

        //initializing the arrays
        for(int i=0; i<g.numOfvertices; i++)
        {
            distance[i] = Integer.MAX_VALUE;//initial distance is infinite
            visited[i] = false;//shortest distance for any node has not been found yet
        }
        distance[src] = 0;

        for(int i=0; i<g.numOfvertices; i++)
        {
            int closestVertex = getClosestVertex(distance, visited);//get the closest node
            //if closest node is infinite distance away, it means that no other node can be reached. So

            if(closestVertex == Integer.MAX_VALUE) {
                return distance;
            }

            visited[closestVertex] = true;
            for(int j=0; j<g.numOfvertices; j++)
            {
                if(visited[j] == false)//shortest distance of the node j should not have been finalized
                {
                    if(g.adjMatrix[closestVertex][j] != 0)
                    {
                        int d = distance[closestVertex] + g.adjMatrix[closestVertex][j];
                        if(d < distance[j])//distance via closestVertex is less than the initial distance
                            distance[j] = d;
                    }
                }
            }
        }
        return distance;





    }

    public static void main(String[] args)


    {
        int numOfVertices = 6;
        int[][] adjMat = new int[6][6];
        Graph graph = new Graph(adjMat, numOfVertices);
        graph.addEdge(0, 4, 21);
        graph.addEdge(0, 3, 18);
        graph.addEdge(2, 1, 7);
        graph.addEdge(1, 4, 6);
        graph.addEdge(4, 5, 10);
        graph.addEdge(4, 3, 11);
        graph.addEdge(5, 3, 7);
        int[] dist = dijkstrasShortestPath(graph, 0);
        System.out.print(Arrays.toString(dist));
    }
}