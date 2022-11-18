package hu.webuni.airport.web;





    public class LychrelNumberTest {
        // Max Iterations
        private static int MAX_ITERATIONS = 50;

        // Function to check whether number is Lychrel Number
        private static boolean isLychrel(long number) {
            for (int i = 0; i < MAX_ITERATIONS; i++) {
                number = number + reverse(number);
                if (isPalindrome(number))
                    return false;

            }
            return true;
        }

        // Function to check whether the number is Palindrome
        private static boolean isPalindrome(final long number) {
            return number == reverse(number);
        }

        // Function to reverse the number
        private static long reverse(long number) {
            long reverse = 0;

            while (number > 0) {
                long remainder = number % 10;
                reverse = (reverse * 10) + remainder;
                number = number / 10;
            }
            return reverse;
        }


        public static void main(String[] args) {
            int counter = 0;
            for (int i = 0; i < 10000; i++) {
                long number = i;
                //System.out.println(number + " is lychrel? "
                  //      + isLychrel(number));
                if (isLychrel(number)) counter++;
                if (counter==200 && isLychrel(i)) System.out.println("ez az: "+i);
            }
            //System.out.println(counter);
        }
    }
