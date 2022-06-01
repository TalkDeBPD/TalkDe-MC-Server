#include <iostream>
#include <list>

using namespace std;

int main()
{
	auto fun = []() { cout << "Hello World!"; };
	cout << typeid(fun).name() << endl;
	list<int> nums = { 24, 45, -38, 15, -64, 24 };
	for (const int& num : nums)
	{
		cout << num << endl;
	}
	return 0;
}
